import { check } from 'k6';
import ws from 'k6/ws';

//const baseUrl = 'ws://tests.cludus.xyz:8080/websocket';
const baseUrl = 'ws://gateway.cludus.xyz/websocket';

export const options = {
    vus: 10000,
    duration: '5m0s',
};

export default function () {
    const timeout = Math.floor(60000 + (Math.random() * 60000));
    console.log('timeout: ' + timeout);
    const params = {
        headers: { 'Authentication': 'k6test' },
    };
    const res = ws.connect(baseUrl, params, function (socket) {
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