from fastapi import APIRouter
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from services.llm_gateway import stream_chat
import asyncio
import json

router = APIRouter(prefix="/compare", tags=["compare"])


class CompareRequest(BaseModel):
    prompt: str
    models: list[str] = ["claude-sonnet-4-6", "deepseek-v3", "gpt-4o"]


@router.post("")
async def compare(req: CompareRequest):
    async def generate():
        async def stream_model(model: str):
            yield f"data: {json.dumps({'model': model, 'status': 'start'})}\n\n"
            full = ""
            async for chunk in stream_chat(
                [{"role": "user", "content": req.prompt}],
                model=model, temperature=0.7, max_tokens=1024
            ):
                yield chunk
                if chunk.startswith("data: "):
                    try:
                        inner = json.loads(chunk[6:].strip())
                        full += inner.get("choices", [{}])[0].get("delta", {}).get("content", "")
                    except Exception:
                        pass
            yield f"data: {json.dumps({'model': model, 'status': 'done', 'content': full})}\n\n"

        tasks = [stream_model(m) for m in req.models]
        queues = [asyncio.Queue() for _ in req.models]

        async def collect(gen, q):
            async for chunk in gen:
                await q.put(chunk)
            await q.put(None)

        collectors = [asyncio.create_task(collect(gen, q)) for gen, q in zip(tasks, queues)]
        active = len(req.models)
        while active > 0:
            for q in queues:
                try:
                    chunk = q.get_nowait()
                    if chunk is None:
                        active -= 1
                    else:
                        yield chunk
                except asyncio.QueueEmpty:
                    pass
            await asyncio.sleep(0.05)
        yield "data: [DONE]\n\n"

    return StreamingResponse(generate(), media_type="text/event-stream")
