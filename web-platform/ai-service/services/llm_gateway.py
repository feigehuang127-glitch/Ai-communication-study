import httpx
from typing import AsyncIterator
from config import settings

MODELS = {
    "claude-sonnet-4-6": {
        "provider": "anthropic",
        "url": "https://api.anthropic.com/v1/messages",
        "api_key": settings.anthropic_api_key,
        "header_name": "x-api-key",
        "header_prefix": "",
    },
    "deepseek-v3": {
        "provider": "deepseek",
        "url": "https://api.deepseek.com/v1/chat/completions",
        "api_key": settings.deepseek_api_key,
        "header_name": "Authorization",
        "header_prefix": "Bearer ",
    },
    "gpt-4o": {
        "provider": "openai",
        "url": "https://api.openai.com/v1/chat/completions",
        "api_key": settings.openai_api_key,
        "header_name": "Authorization",
        "header_prefix": "Bearer ",
    },
}


async def stream_chat(
    messages: list[dict],
    model: str = None,
    temperature: float = 0.7,
    max_tokens: int = 2048,
) -> AsyncIterator[str]:
    model_name = model or settings.default_model
    cfg = MODELS.get(model_name)
    if not cfg:
        yield f"data: [ERROR] Unknown model: {model_name}\n\n"
        return

    headers = {
        cfg["header_name"]: f'{cfg["header_prefix"]}{cfg["api_key"]}',
        "Content-Type": "application/json",
    }

    if cfg["provider"] == "anthropic":
        system_msg = next((m["content"] for m in messages if m["role"] == "system"), "")
        chat_messages = [m for m in messages if m["role"] != "system"]
        body = {
            "model": model_name,
            "max_tokens": max_tokens,
            "temperature": temperature,
            "system": system_msg,
            "messages": chat_messages,
            "stream": True,
        }
    else:
        body = {
            "model": model_name,
            "messages": messages,
            "max_tokens": max_tokens,
            "temperature": temperature,
            "stream": True,
        }

    async with httpx.AsyncClient(timeout=60.0) as client:
        try:
            async with client.stream("POST", cfg["url"], headers=headers, json=body) as resp:
                resp.raise_for_status()
                async for line in resp.aiter_lines():
                    if line.startswith("data: "):
                        yield f"{line}\n\n"
        except httpx.HTTPStatusError as e:
            yield f"data: [ERROR] HTTP {e.response.status_code}: {e.response.text[:200]}\n\n"
        except Exception as e:
            yield f"data: [ERROR] {str(e)}\n\n"


async def chat_sync(
    messages: list[dict],
    model: str = None,
    temperature: float = 0.7,
    max_tokens: int = 2048,
) -> str:
    """Non-streaming chat for internal calls"""
    parts = []
    async for chunk in stream_chat(messages, model, temperature, max_tokens):
        parts.append(chunk)
    return "".join(parts)
