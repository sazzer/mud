import Glue from 'glue';

const manifest = {
    server: {
        
    },
    connections: [
        {
            port: 3000,
            labels: ['web']
        }
    ],
    registrations: [{
        plugin: {
            register: 'hapi-info',
            options: {
                path: '/api/info'
            }
        }
    }, {
        plugin: 'hapi-routes-status',
        options: {
            routes: {
                prefix: '/api'
            }
        }
    }, {
        plugin: 'inert'
    }, {
        plugin: 'vision'
    }, {
        plugin: 'blipp'
    }, {
        plugin: {
            register: 'hapi-alive',
            options: {
                path: '/api/health',
                tags: ['health', 'monitor', 'api'],
                healthCheck: (server, callback) => {
                    callback();
                }
            }
            
        }
    }, {
        plugin: {
            register: 'good',
            options: {
                opsInterval: 30000,
                responsePayload: true,
                reporters: [{
                    reporter: 'good-console',
                    events: {
                        ops: '*',
                        request: '*',
                        response: '*',
                        log: '*',
                        error: '*'
                    }
                }]
            }
        }
    }, {
        plugin: {
            register: 'hapi-swaggered',
            options: {
                endpoint: '/api/swagger',
                info: {
                    title: 'WebMUD',
                    description: 'Web based MUD',
                    version: '1.0'
                },
                tagging: {
                    mode: 'path',
                    
                },
                routeTags: ['api']
            }
        }
    }, {
        plugin: {
            register: 'hapi-swaggered-ui',
            options: {
                path: '/api/docs'
            }
        }
    }, {
        plugin: {
            register: 'hapi-route-glob-reg',
            options: {
                directory: [
                    `${__dirname}/routes/**`
                ]
            }
        }
    }]
}

export function startServer() {
    return new Promise((resolve, reject) => {
        const options = {
            relativeTo: __dirname
        };
        
        Glue.compose(manifest, options, (err, server) => {
            if (err) {
                reject(err);
            } else {
                server.start(() => {
                    resolve(server);
                });
            }
        })
    });
}
