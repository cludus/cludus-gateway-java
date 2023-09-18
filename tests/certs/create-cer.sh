#!/bin/bash

root_domain_name=cludus.dev
root_file_name=root-ca

function clean_all {
rm *.pem
rm *.key
rm *.crt
rm *.csr
rm *.p12
}

function create_ca_cert {
openssl req -x509 \
            -sha256 -days 3560 \
            -nodes \
            -newkey rsa:2048 \
            -subj "/CN=$root_domain_name/C=XX/L=Home" \
            -keyout $root_file_name.key -out $root_file_name.crt

cat $root_file_name.key $root_file_name.crt > $root_file_name.pem

openssl pkcs12 -export \
               -inkey $root_file_name.key \
               -in $root_file_name.crt \
               -passout pass: \
               -out $root_file_name.p12

sudo cp root-ca.crt /usr/local/share/ca-certificates/
sudo update-ca-certificates
}

function create_cert {
openssl req -new \
            -newkey rsa:4096 \
            -keyout $1.key \
            -out $1.csr \
            -sha256 \
            -nodes \
            -subj "/C=XX/ST=XX/L=Home/O=XX/OU=XX/CN=$2"

openssl x509 -req \
    -in $1.csr \
    -CA $root_file_name.crt -CAkey $root_file_name.key \
    -CAcreateserial -out $1.crt \
    -extfile gateway-cludus-dev.ext \
    -days 3650 \
    -sha256

cat $1.key $1.crt $root_file_name.crt > $1.pem
openssl pkcs12 -export \
               -inkey $1.key \
               -in $1.crt \
               -passout pass: \
               -out ./$1.p12
}

# clean_all
# create_ca_cert
# create_cert "gateway-cludus-dev" "gateway.cludus.dev"
# create_cert "metrics-cludus-dev" "metrics.cludus.dev"
