package dayone.security;


import dayone.dayone.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final long TOKEN_EXPIRE_TIME = 1000 * 60; // 1시간    private static final String KEY_ROLES = "roles";
    private static final String KEY_ROLES = "roles";
    private final MemberService memberService;

    @Value("${spring.jwt.secret}")
    private String secretKey;


    public  String generateToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(KEY_ROLES, roles);

        var now = new Date();
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        /** 토큰 생성(발급)
         * @param username
         * @param roles
         * @return
         */


        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)   //토큰 생성시간
                .setExpiration(expiredDate) //토근 만료시간
                .signWith(SignatureAlgorithm.ES512, this.secretKey)// 사용할 암호화알고리즘,비밀키
                .compact();

    }
    public Authentication getAuthentication(String jwt){
        UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));
        //Not implemented yet
        return new UsernamePasswordAuthentionToken(userDetails, "", userDetails.getAuthorities());
}
      public String getUsername(String token) {
          return this.paeseClaims(token).getSubject();
      }

      public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;

          var claims = this.paeseClaims(token);
          return !claims.getExpiration().before(new Date());

      }



       private Claims paeseClaims(String token) {
        try{
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        }catch (ExpiredJwtException e){
            // TODO
            return e.getClaims();
        }
       }


}