import httpx
from config import settings


class RagEngine:
    def __init__(self):
        self._cache: dict[str, str] = {}

    async def load_course_context(self, course_slug: str) -> str:
        if course_slug in self._cache:
            return self._cache[course_slug]

        try:
            async with httpx.AsyncClient() as client:
                resp = await client.get(
                    f"{settings.backend_url}/api/courses/{course_slug}"
                )
                if resp.status_code == 200:
                    data = resp.json()
                    text = self._flatten_course(data)
                    self._cache[course_slug] = text
                    return text
        except Exception:
            pass
        return ""

    def _flatten_course(self, data: dict) -> str:
        course = data.get("course", {})
        parts = [
            f"# {course.get('title', '')}",
            course.get("description", ""),
        ]
        for ch_data in data.get("chapters", []):
            chapter = ch_data.get("chapter", {})
            parts.append(f"## {chapter.get('title', '')}")
            for lesson in ch_data.get("lessons", []):
                parts.append(f"### {lesson.get('title', '')}")
                content = lesson.get("content", "{}")
                import json
                try:
                    content_obj = json.loads(content) if isinstance(content, str) else content
                    parts.append(content_obj.get("body", ""))
                except json.JSONDecodeError:
                    parts.append(str(content))
        return "\n\n".join(parts)

    def inject_context(self, system_prompt: str, course_slug: str) -> str:
        if course_slug in self._cache:
            ctx = self._cache[course_slug][:2000]
            return f"{system_prompt}\n\n## 当前课程资料（供参考）\n{ctx}"
        return system_prompt


rag_engine = RagEngine()
