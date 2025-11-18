FROM python:3.9-slim

WORKDIR /cdr-multislip-excesslogic

COPY requirements.txt .

RUN python -m pip install --upgrade pip

RUN pip install -r requirements.txt 

RUN pip install --no-cache-dir --upgrade pip \
    && pip install --no-cache-dir opencv-python-headless==4.11.0.86

COPY . .

ENV PYTHONFAULTHANDLER=1

EXPOSE 8519

ENTRYPOINT [ "python","azure_ocr_multislip.py" ]