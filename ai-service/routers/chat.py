from fastapi import APIRouter, Query
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from services.llm_gateway import stream_chat
from services.persona_router import get_persona

router = APIRouter(prefix="/chat", tags=["chat"])


class ChatRequest(BaseModel):
    message: str
    context_page: str = ""
    conversation_history: list[dict] = []
    model: str = ""


@router.post("")
async def chat(req: ChatRequest):
    persona = get_persona(req.context_page, req.message)

    messages = [{"role": "system", "content": persona.system_prompt}]
    messages.extend(req.conversation_history)
    messages.append({"role": "user", "content": req.message})

    async def generate():
        yield f"data: {persona.persona_id}|{persona.name}\n\n"
        async for chunk in stream_chat(messages, model=req.model, temperature=persona.temperature):
            yield chunk
        yield "data: [DONE]\n\n"

    return StreamingResponse(generate(), media_type="text/event-stream")
