import os

print(" DOCUMENT CHUNKING ENGINE")
print("="*40)

def chunk_text(text, size=500, overlap=100):
    """Smart chunking with overlap for context preservation"""
    chunks = []
    start = 0

    while start < len(text):
        end = min(start + size, len(text))
        chunk = text[start:end]
        chunks.append(chunk)

        if end >= len(text):
            break

        start += size - overlap

    return chunks

# Process sample document
with open('../simple-rag-explained/amazon_return_policy.txt', 'r') as file:
    sample_doc = file.read().replace('\n', '')

print(f" Original document: {len(sample_doc)} characters")
print("-"*40)

chunks = chunk_text(sample_doc, size=500, overlap=100)

print(f" Created {len(chunks)} chunks")
print("-"*40)

for i, chunk in enumerate(chunks, 1):
    print(f"\nChunk {i} ({len(chunk)} chars):")
    print(f"Preview: {chunk[:60]}...")

# Save verification
with open('../chunk-test.txt', 'w') as f:
    f.write(f"CHUNKS:{len(chunks)}")

print("\n" + "="*40)
print(" Chunking complete!")
print(f" Stats: {len(chunks)} chunks from {len(sample_doc)} chars")
print(" Ready for vectorization!")