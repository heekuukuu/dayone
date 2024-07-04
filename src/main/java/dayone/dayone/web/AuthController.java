package dayone.dayone.web;

import dayone.dayone.model.Auth;
import dayone.dayone.persist.MemberRepository;
import dayone.dayone.service.MemberService;
import dayone.security.TokenProvider;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@AllArgsConstructor
@RequestMapping("/auth")
@RequiredArgsConstructor

public class AuthController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        var result = this.memberService.register(request);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/signin")// 패스워드 검증 , 토큰생성
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        var member = this.memberService.authenticate(request);
        var token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());
          log.info("user login -> " + request.getUsername());
        return ResponseEntity.ok(token);

}}