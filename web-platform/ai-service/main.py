from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers import health, chat

app = FastAPI(title="AI Learning Platform - AI Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router)
app.include_router(chat.router)
