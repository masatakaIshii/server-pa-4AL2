FROM gcc

WORKDIR /app

EXPOSE 8080

USER root

ADD . .

RUN chmod a+x ./main.c
RUN chmod a+x ./launch.sh

ENTRYPOINT ["./launch.sh"]
