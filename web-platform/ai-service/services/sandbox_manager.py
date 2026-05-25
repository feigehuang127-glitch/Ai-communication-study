import docker
import uuid
import asyncio
from datetime import datetime, timedelta
from config import settings

client = docker.from_env()


class SandboxManager:
    def __init__(self):
        self._active: dict[str, dict] = {}

    async def create_container(
        self, user_id: int, image: str = "python:3.12-slim", template: str = ""
    ) -> dict:
        container_name = f"sandbox-{user_id}-{uuid.uuid4().hex[:8]}"
        try:
            container = client.containers.run(
                image=image,
                name=container_name,
                command="tail -f /dev/null",
                detach=True,
                mem_limit=settings.sandbox_memory_limit,
                nano_cpus=settings.sandbox_cpu_limit,
                network_disabled=False,
                remove=True,
            )
            session = {
                "container_id": container.id,
                "container_name": container_name,
                "user_id": user_id,
                "image": image,
                "created_at": datetime.now(),
                "expires_at": datetime.now() + timedelta(seconds=settings.sandbox_idle_timeout),
            }
            self._active[container.id] = session
            return session
        except Exception as e:
            return {"error": str(e)}

    async def execute_code(self, container_id: str, code: str, language: str = "python") -> dict:
        session = self._active.get(container_id)
        if not session:
            return {"error": "Session not found"}

        try:
            container = client.containers.get(container_id)
            if language == "python":
                result = container.exec_run(
                    ["python", "-c", code],
                    timeout=settings.sandbox_timeout,
                )
            elif language == "javascript":
                result = container.exec_run(
                    ["node", "-e", code],
                    timeout=settings.sandbox_timeout,
                )
            else:
                return {"error": f"Unsupported language: {language}"}

            return {
                "stdout": result.output.decode("utf-8", errors="replace")[:5000],
                "stderr": "",
                "exit_code": result.exit_code,
            }
        except Exception as e:
            return {"error": str(e)}

    async def stop_container(self, container_id: str) -> bool:
        session = self._active.pop(container_id, None)
        if not session:
            return False
        try:
            container = client.containers.get(container_id)
            container.stop(timeout=5)
            return True
        except Exception:
            return False

    async def cleanup_expired(self):
        now = datetime.now()
        expired = [cid for cid, s in self._active.items() if s["expires_at"] < now]
        for cid in expired:
            await self.stop_container(cid)


sandbox_manager = SandboxManager()
