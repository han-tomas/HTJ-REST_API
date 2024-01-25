package htj.hantomas.htjrestapi.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {
    @Test
    public void builder(){ // build메서드를 이용한 방식
        Event event = Event.builder()
                .name("Inflearn Spring REST API")
                .description("REST API development with Spring")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean(){ // 기본 생성자(default 생성자)를 이용한 방식
        // Given
        String name = "Event";
        String description = "Spring";
        // When
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);
        // Then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }
}