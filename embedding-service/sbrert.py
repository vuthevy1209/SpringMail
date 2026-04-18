from sentence_transformers import SentenceTransformer
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List

class TextRequest(BaseModel):
    text: str

class BatchTextRequest(BaseModel):
    texts: List[str]

app = FastAPI()
# Model 'paraphrase-multilingual-mpnet-base-v2' mạnh hơn, 768 chiều, hỗ trợ Tiếng Việt cực tốt
model = SentenceTransformer('paraphrase-multilingual-mpnet-base-v2')

@app.post("/embed")
async def get_embedding(request: TextRequest):
    vector = model.encode(request.text).tolist()
    return vector

@app.post("/embed/batch")
async def get_batch_embedding(request: BatchTextRequest):
    vectors = model.encode(request.texts).tolist()
    return vectors