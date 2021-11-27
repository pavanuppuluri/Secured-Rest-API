package org.secureapp.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.secureapp.model.TokenStore;
import org.secureapp.repository.BlackListedTokenRepository;
import org.secureapp.util.LoggedInUserHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider implements Serializable {

    @Value("${jwt.token.validity}")
    public long TOKEN_VALIDITY;

    @Value("${jwt.signing.key}")
    public String SIGNING_KEY;

    @Value("${jwt.authorities.key}")
    public String AUTHORITIES_KEY;

    @Value("${jwt.token.prefix}")
    public String TOKEN_PREFIX;

    @Autowired
    BlackListedTokenRepository blackListedTokenRepository;

    @Autowired
    LoggedInUserHelper loggedInUserHelper;

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        SecretKey key = Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY * 1000))
                .signWith(key)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public UsernamePasswordAuthenticationToken getAuthenticationToken(final String token, final Authentication existingAuth, final UserDetails userDetails) {

        SecretKey key = Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8));

        final JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(key).build();

        final Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);

        final Claims claims = claimsJws.getBody();

        final Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public boolean checkIfTokenBlackListed(String token) {
        if (token != null) {
            String userName = getUsernameFromToken(token);
            TokenStore tokenStore = blackListedTokenRepository.findByToken(token);

            if (tokenStore != null) {
                String tokenFromDb = tokenStore.getToken();

                return token.equals(tokenFromDb);
            }

        }

        return false;
    }

    public void blackListTokenOnLogout(String token) {
        TokenStore tokenStore = new TokenStore();
        token = token.replace(TOKEN_PREFIX, "").trim();
        if (token != null) {
            String userName = getUsernameFromToken(token);
            tokenStore.setToken(token);
            tokenStore.setUsername(userName);
            blackListedTokenRepository.save(tokenStore);
        }
    }

    public void blackListTokenOnDelete(String token, String userName) {
        TokenStore tokenStore = new TokenStore();
        token = token.replace(TOKEN_PREFIX, "").trim();
        if (token != null) {

            if (userName.equalsIgnoreCase(loggedInUserHelper.getCurrentLoggedInUserName())) {
                tokenStore.setToken(token);
                tokenStore.setUsername(userName);
                blackListedTokenRepository.save(tokenStore);
            }
        }
    }

}