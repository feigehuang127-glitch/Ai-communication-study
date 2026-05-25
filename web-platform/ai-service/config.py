from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    anthropic_api_key: str = ""
    openai_api_key: str = ""
    deepseek_api_key: str = ""
    redis_host: str = "localhost"
    redis_port: int = 6379
    sandbox_memory_limit: str = "128m"
    sandbox_cpu_limit: int = 500_000_000
    sandbox_timeout: int = 300
    sandbox_idle_timeout: int = 1800

    model_config = {"env_file": ".env", "extra": "ignore"}

settings = Settings()
