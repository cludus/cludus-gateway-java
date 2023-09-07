import { check } from 'k6';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';
import http from 'k6/http';
import ws from 'k6/ws';

const baseUrl = 'ws://localhost:8080/websocket';

export const options = {
    vus: 1000,
    duration: '20m0s',
};

export default function () {
    const res = ws.connect(baseUrl, {}, function (socket) {
        socket.on('open', () => {
            console.log('connected')
            socket.setInterval(() => {
                socket.send(Date.now())
            }, Math.floor(Math.random() * 10000))
        });
        socket.on('message', (data) => console.log('Message received: ', data));
        socket.on('close', () => console.log('disconnected'));
    });

    check(res, {'status is 101': (r) => r && r.status === 101});
}