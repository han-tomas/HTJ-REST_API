package htj.hantomas.htjrestapi.events;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.time.LocalDateTime;
@Builder @AllArgsConstructor @NoArgsConstructor
@Getter @Setter @EqualsAndHashCode(of = "id")
/*
@AllArgsConstructor @NoArgsConstructor:
    기본 생성자와 모든 Argument를 가지고 있는 생성자 둘 다 만들기 위해 선언

@Setter @EqualsAndHashCode(of = "id")
    EqualsOf와HashCodeOf를 사용할때 기본적으로 모든 필드를 사용한다.
    그러나 나중에 Entity간의 연관관계가 있을 때 ,
    연관관계가 상호 참조하는 관계가 되어버리면 Equals와 HashCode를 구현한 코드 안에서
    StackOverFlow가 발생할 수 있다.
    그래서 id의 값만 가지고 Equals와 HashCode를 비교하도록 만들어서 사용
    원하면 몇가지 필드를 더 추가해서 사용가능 (of = {"","",""}) 형식으로

@Data를 쓰지 않는 이유
     Equals와 HashCode를 구현해 주지만 모든 property를 다 써서 구현하기 때문에
     Entity에 @Data를 쓰면 안된다. => StackOverFlow
 */
@Entity

public class Event {
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location; // (optional) 이게 없으면 온라인 모임
    private int basePrice; // (optional)
    private int maxPrice; // (optional)
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;

}
