# ============================================================
# Frontend Dockerfile — React + Vite (development server)
# Runs the Vite dev server bound to 0.0.0.0 on port 5173
# ============================================================

FROM node:20-alpine
WORKDIR /app

# Install dependencies first (cached unless package files change)
COPY package.json package-lock.json* ./
RUN npm install

# Copy the rest of the source
COPY . .

EXPOSE 5173

# Bind to 0.0.0.0 so the browser on the host can reach the dev server
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5173"]
