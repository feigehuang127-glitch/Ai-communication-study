import os
from dataclasses import dataclass

PERSONAS = {
    "lecturer": {
        "name": "讲解老师",
        "icon": "teacher",
        "temperature": 0.7,
        "prompt_file": "prompts/lecturer.txt",
    },
    "code_mentor": {
        "name": "代码导师",
        "icon": "code",
        "temperature": 0.3,
        "prompt_file": "prompts/code_mentor.txt",
    },
    "study_buddy": {
        "name": "陪练同学",
        "icon": "buddy",
        "temperature": 0.9,
        "prompt_file": "prompts/study_buddy.txt",
    },
    "analyst": {
        "name": "学习分析师",
        "icon": "chart",
        "temperature": 0.5,
        "prompt_file": "prompts/analyst.txt",
    },
}

PAGE_TO_PERSONA = {
    "/college": "lecturer",
    "/course": "lecturer",
    "/lab": "code_mentor",
    "/sandbox": "code_mentor",
    "/game": "study_buddy",
    "/play": "study_buddy",
    "/profile": "analyst",
    "/wrongbook": "analyst",
}

FALLBACK_PERSONA = "lecturer"


@dataclass
class PersonaConfig:
    persona_id: str
    name: str
    temperature: float
    system_prompt: str


def load_prompt(filename: str) -> str:
    base_dir = os.path.dirname(os.path.abspath(__file__))
    prompt_path = os.path.join(base_dir, "..", filename)
    try:
        with open(prompt_path, "r", encoding="utf-8") as f:
            return f.read()
    except FileNotFoundError:
        return ""


def get_persona(context_page: str = "", user_intent: str = "") -> PersonaConfig:
    persona_id = FALLBACK_PERSONA
    for path_prefix, pid in PAGE_TO_PERSONA.items():
        if path_prefix in context_page:
            persona_id = pid
            break

    intent_map = {
        "为什么": "lecturer",
        "解释": "lecturer",
        "debug": "code_mentor",
        "代码": "code_mentor",
        "提示": "study_buddy",
        "挑战": "study_buddy",
        "分析": "analyst",
        "报告": "analyst",
    }
    for keyword, pid in intent_map.items():
        if keyword in user_intent:
            persona_id = pid
            break

    cfg = PERSONAS[persona_id]
    system_prompt = load_prompt(cfg["prompt_file"])
    return PersonaConfig(
        persona_id=persona_id,
        name=cfg["name"],
        temperature=cfg["temperature"],
        system_prompt=system_prompt,
    )
