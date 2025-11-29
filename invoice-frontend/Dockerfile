FROM node:18
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

RUN npm run build
RUN npm install -g serve

EXPOSE 7024
CMD ["serve", "-s", "build", "-l", "7024"]

