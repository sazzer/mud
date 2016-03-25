import {expect} from 'chai';
import moment from 'moment-timezone';
import sinon from 'sinon';
import {Token} from '../token';
import {encodeToken, __RewireAPI__ as TokenEncoderRewire} from '../tokenEncoder';

describe('TokenEncoder', function() {
    describe('encodeToken', function() {
        describe('valid token', function() {
            const start = moment().add(30, 'minutes');
            const end = moment().add(1, 'hours');
            const tokenId = 'thisIsTheTokenId';
            const userId = 'thisIsTheUserId';
            const signedToken = 'thisIsTheSignedToken';

            const token = new Token({startTime: start,
                expiryTime: end,
                tokenId,
                userId});

            it('should attempt to generate a valid JWT', function() {
                const stubJwt = sinon.stub();
                stubJwt.callsArgWith(3, signedToken);

                TokenEncoderRewire.__Rewire__('jwt', {
                    sign: stubJwt
                });
                return encodeToken(token).then(function(jwt) {
                    expect(stubJwt.callCount).to.equal(1);
                    expect(stubJwt.firstCall.args[0]).to.deep.equal({});
                    expect(stubJwt.firstCall.args[1]).to.equal('abcdef');
                    expect(stubJwt.firstCall.args[2]).to.have.all.keys(
                        'algorithm',
                        'audience',
                        'issuer',
                        'expiresIn',
                        'notBefore',
                        'jwtid',
                        'subject'
                    );
                    expect(stubJwt.firstCall.args[2]).to.have.property('algorithm', 'HS256');
                    expect(stubJwt.firstCall.args[2]).to.have.property('audience', 'uk.co.grahamcox.mud.jwt');
                    expect(stubJwt.firstCall.args[2]).to.have.property('issuer', 'uk.co.grahamcox.mud.jwt');
                    expect(stubJwt.firstCall.args[2]).to.have.property('jwtid', tokenId);
                    expect(stubJwt.firstCall.args[2]).to.have.property('subject', userId);
                    expect(stubJwt.firstCall.args[2]).to.have.property('expiresIn').within(3598, 3602);
                    expect(stubJwt.firstCall.args[2]).to.have.property('notBefore').within(1798, 1802);

                    expect(jwt).to.equal(signedToken);
                });
            });

            after(function() {
                TokenEncoderRewire.__ResetDependency__('jwt');
            });
        });
    });
});
