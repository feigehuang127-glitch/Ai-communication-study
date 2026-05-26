import docker
import uuid
import asyncio
import os
import re
from datetime import datetime, timedelta
from config import settings

# Only connect to Docker if sandbox is explicitly enabled.
# Mounting docker.sock in production is a container-escape risk;
# use gVisor (runsc), Firecracker, or Sysbox for multi-tenant isolation.
SANDBOX_ENABLED = os.getenv("SANDBOX_ENABLED", "false").lower() in ("true", "1", "yes")
client = docker.from_env() if SANDBOX_ENABLED else None

FORBIDDEN_PATTERNS = [
    r"os\.system\s*\(",
    r"subprocess\.",
    r"__import__\s*\(",
    r"eval\s*\(",
    r"exec\s*\(",
    r"compile\s*\(",
    r"ctypes\.",
    r"importlib\.import_module",
    r"open\s*\([^)]*['\"]w",
    r"shutil\.",
    r"pathlib\.Path\.unlink",
    r"pathlib\.Path\.rmdir",
    r"os\.remove\(",
    r"os\.rmdir\(",
    r"os\.chmod\(",
    r"os\.chown\(",
    r"os\.kill\(",
    r"os\.fork\(",
    r"signal\.",
    r"socket\.",
    r"requests\.",
    r"urllib\.",
    r"http\.client",
    r"ftplib\.",
    r"smtplib\.",
    r"telnetlib\.",
]


class SandboxManager:
    def __init__(self):
        self._active: dict[str, dict] = {}

    def _validate_code(self, code: str) -> str | None:
        """Return an error message if dangerous patterns are found, else None."""
        for pattern in FORBIDDEN_PATTERNS:
            if re.search(pattern, code):
                return f"Blocked dangerous pattern: {pattern}"
        return None

    async def create_container(
        self, user_id: int, image: str = "python:3.12-slim", template: str = ""
    ) -> dict:
        if not SANDBOX_ENABLED or client is None:
            return {"error": "Sandbox disabled. Set SANDBOX_ENABLED=true to enable."}

        container_name = f"sandbox-{user_id}-{uuid.uuid4().hex[:8]}"
        try:
            container = client.containers.run(
                image=image,
                name=container_name,
                command="tail -f /dev/null",
                detach=True,
                mem_limit=settings.sandbox_memory_limit,
                nano_cpus=settings.sandbox_cpu_limit,
                network_disabled=True,
                security_opt=["no-new-privileges:true"],
                read_only=True,
                tmpfs={"/tmp": "rw,noexec,nosuid,size=64m"},
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
        if not SANDBOX_ENABLED or client is None:
            return {"error": "Sandbox disabled."}

        validation_error = self._validate_code(code)
        if validation_error:
            return {"error": validation_error}

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
            if client:
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
