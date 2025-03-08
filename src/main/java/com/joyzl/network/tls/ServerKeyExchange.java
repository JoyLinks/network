package com.joyzl.network.tls;

/**
 * TLS 1.0
 * 
 * <pre>
 * RFC2246
 * 
    enum { rsa, diffie_hellman } KeyExchangeAlgorithm;

       struct {
           opaque rsa_modulus<1..2^16-1>;
           opaque rsa_exponent<1..2^16-1>;
       } ServerRSAParams;

       rsa_modulus The modulus of the server's temporary RSA key.
       rsa_exponent The public exponent of the server's temporary RSA key.

       struct {
           opaque dh_p<1..2^16-1>;
           opaque dh_g<1..2^16-1>;
           opaque dh_Ys<1..2^16-1>;
       } ServerDHParams;     // Ephemeral DH parameters

       dh_p The prime modulus used for the Diffie-Hellman operation.
       dh_g The generator used for the Diffie-Hellman operation.
       dh_Ys The server's Diffie-Hellman public value (g^X mod p).
       
       struct {
           select (KeyExchangeAlgorithm) {
               case diffie_hellman:
                   ServerDHParams params;
                   Signature signed_params;
               case rsa:
                   ServerRSAParams params;
                   Signature signed_params;
           };
       } ServerKeyExchange;

       params The server's key exchange parameters.
       signed_params
           For non-anonymous key exchanges, a hash of the corresponding
           params value, with the signature appropriate to that hash
           applied.
       md5_hash MD5(ClientHello.random + ServerHello.random + ServerParams);
       sha_hash SHA(ClientHello.random + ServerHello.random + ServerParams);

       enum { anonymous, rsa, dsa } SignatureAlgorithm;

       select (SignatureAlgorithm)
       {   case anonymous: struct { };
           case rsa:
               digitally-signed struct {
                   opaque md5_hash[16];
                   opaque sha_hash[20];
               };
           case dsa:
               digitally-signed struct {
                   opaque sha_hash[20];
               };
       } Signature;
 * </pre>
 */
class ServerKeyExchange extends Handshake {

	@Override
	public byte msgType() {
		return SERVER_KEY_EXCHANGE;
	}
}