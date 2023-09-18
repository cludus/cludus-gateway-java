#!/bin/bash

root_domain_name=cludus.dev
root_file_name=root-ca
domain_name=gateway.cludus.dev
file_name=gateway-cludus-dev

rm *.pem
rm *.key
rm *.crt
rm *.csr
rm *.p12

openssl req -x509 \
            -sha256 -days 3560 \
            -nodes \
            -newkey rsa:2048 \
            -subj "/CN=$root_domain_name/C=XX/L=Home" \
            -keyout $root_file_name.key -out $root_file_name.crt

openssl req -new \
            -newkey rsa:4096 \
            -keyout $file_name.key \
            -out $file_name.csr \
            -sha256 \
            -nodes \
            -subj "/C=XX/ST=XX/L=Home/O=XX/OU=XX/CN=$domain_name"

openssl x509 -req \
    -in $file_name.csr \
    -CA $root_file_name.crt -CAkey $root_file_name.key \
    -CAcreateserial -out $file_name.crt \
    -extfile gateway-cludus-dev.ext \
    -days 3650 \
    -sha256

cat $root_file_name.key $root_file_name.crt > $root_file_name.pem
openssl pkcs12 -export \
               -inkey $root_file_name.key \
               -in $root_file_name.crt \
               -passout pass: \
               -out $root_file_name.p12

cat $file_name.key $file_name.crt $root_file_name.crt > $file_name.pem
openssl pkcs12 -export \
               -inkey $file_name.key \
               -in $file_name.crt \
               -passout pass: \
               -out ./$file_name.p12
