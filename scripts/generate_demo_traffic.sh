#!/usr/bin/env bash

TARGET="127.0.0.1"

echo "=== JavaGuard demo traffic generator ==="

echo "[1/4] HTTP traffic"

for i in $(seq 1 40); do
    curl -s \
        "http://${TARGET}:8080/api/v1/dashboard/summary" \
        > /dev/null || true
done


echo "[2/4] ICMP traffic"

ping \
    -c 20 \
    -i 0.05 \
    "${TARGET}" \
    > /dev/null || true


echo "[3/4] Port scan simulation"

for port in $(seq 20000 20025); do

    timeout 0.15 \
        bash -c \
        "echo >/dev/tcp/${TARGET}/${port}" \
        2>/dev/null || true

done


echo "Waiting for detection window..."

sleep 6


echo "[4/4] SYN flood simulation"

for i in $(seq 1 70); do

    timeout 0.1 \
        bash -c \
        "echo >/dev/tcp/${TARGET}/25000" \
        2>/dev/null || true

done


if command -v nc >/dev/null 2>&1; then

    echo "[extra] UDP traffic"

    for port in 30000 30001 30002 30003 30004; do

        for i in $(seq 1 5); do

            printf "javaguard-demo-%s\n" "$i" |
                nc \
                    -u \
                    -w 1 \
                    "${TARGET}" \
                    "${port}" \
                    >/dev/null 2>&1 || true

        done

    done

else

    echo "netcat not installed - UDP demo skipped"

fi


echo "=== Demo traffic complete ==="