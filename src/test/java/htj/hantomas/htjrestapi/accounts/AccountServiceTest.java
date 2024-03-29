package htj.hantomas.htjrestapi.accounts;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Test
    public void findByUsername(){
        // Given
        String password ="taejun";
        String username = "taejun@email.com";
        Account account = Account.builder()
                .email(username)
                .password("taejun")
                .roles(Set.of(AccountRole.ADMIN,AccountRole.USER))
                .build();

        this.accountService.saveAccount(account);

        // When
        UserDetailsService userDetailsService = accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        //Then
        assertThat(this.passwordEncoder.matches(password, userDetails.getPassword())).isTrue();
    }
    
    @Test //Junit5 버전의 예외 테스트
    public void findByUsernameFail(){
        //Expected
        String username = "random@email.com";
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            accountService.loadUserByUsername(username);
        });

        // When
        assertThat(exception.getMessage()).contains(username);

    }
}