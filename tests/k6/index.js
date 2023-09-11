import { check } from 'k6';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';
import http from 'k6/http';
import ws from 'k6/ws';

const baseUrl = 'ws://cludus.xyz:8080/websocket';
//const baseUrl = 'ws://localhost:8080/websocket';

export const options = {
    vus: 10000,
    duration: '5m0s',
};

export default function () {
    const timeout = Math.floor(60000 + (Math.random() * 60000));
    console.log('timeout: ' + timeout);

    const res = ws.connect(baseUrl, {}, function (socket) {
        socket.on('open', () => {
            console.log('connected')
            socket.setInterval(() => {
                socket.send(Date.now())
            }, timeout)
        });
        socket.on('message', (data) => console.log('Message received: ', data));
        socket.on('close', () => console.log('disconnected'));
    });

    check(res, {'status is 101': (r) => r && r.status === 101});
}