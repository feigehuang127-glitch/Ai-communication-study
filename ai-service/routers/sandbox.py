from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from services.sandbox_manager import sandbox_manager

router = APIRouter(prefix="/sandbox", tags=["sandbox"])


class SandboxCreateRequest(BaseModel):
    user_id: int
    image: str = "python:3.12-slim"
    template: str = ""


class CodeExecuteRequest(BaseModel):
    container_id: str
    code: str
    language: str = "python"


@router.post("/create")
async def create_sandbox(req: SandboxCreateRequest):
    result = await sandbox_manager.create_container(req.user_id, req.image, req.template)
    if "error" in result:
        raise HTTPException(status_code=500, detail=result["error"])
    return result


@router.post("/execute")
async def execute_code(req: CodeExecuteRequest):
    result = await sandbox_manager.execute_code(req.container_id, req.code, req.language)
    return result


@router.delete("/{container_id}")
async def destroy_sandbox(container_id: str):
    ok = await sandbox_manager.stop_container(container_id)
    return {"success": ok}
