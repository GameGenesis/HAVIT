# FOR FRONT-END DEPLOYMENT... (REACT)
FROM node:16-alpine as build-step
WORKDIR /app
ENV PATH /app/web/node_modules/.bin:$PATH
COPY web ./web
WORKDIR /app/web
RUN yarn install
RUN yarn build

# FOR BACK-END DEPLOYMENT... (FLASK)
FROM python:3.10.4-slim
WORKDIR /
# Don't forget "--from"! It acts as a bridge that connects two seperate stages
COPY --from=build-step app ./app
WORKDIR /app
RUN apt-get update && apt-get install -y python3-dev
COPY server ./server
WORKDIR /app/server
RUN pip3 install -r ./requirements.txt
# Pretty much pass everything in the root folder except for the client folder, as we do NOT want to overwrite the pre-generated client folder that is already in the ./app folder
EXPOSE 5000
ENTRYPOINT ["python"]
CMD ["api.py"]

# THIS IS CALLED MULTI-STAGE BUILDING IN DOCKER