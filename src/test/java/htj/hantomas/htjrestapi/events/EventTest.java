package htj.hantomas.htjrestapi.events;

import junitparams.JUnitParamsRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
@RunWith(JUnitParamsRunner.class)
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

    @ParameterizedTest(name = "{index} => basePrice={0}, maxPrice={1}, isFree={2}")
    /*@CsvSource({
            "0, 0, true",
            "100, 0, false",
            "0, 100, false"
    })*/
    @MethodSource("paramsForTestFree")
    public void testFree(int basePrice, int maxPrice, boolean isFree){

        //Given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();
        //When
        event.update();

        //Then
        assertThat(event.isFree()).isEqualTo(isFree);
        /*
            JunitParams를 통해 중복되는 코드를 줄였다.
         */
    }
    private static Object[] paramsForTestFree(){
        return new Object[]{
                new Object[] {0, 0, true},
                new Object[] {100, 0, false},
                new Object[] {0, 100, false},
                new Object[] {100, 200, false}
        };
    }
    // 외국 문서들에서는 스트림을 활용하는 경우가 많았다.
    /*
        private static Stream<Arguments> sumProvider() {
            return Stream.of(
                    Arguments.of(1, 1, 2),
                    Arguments.of(2, 3, 5)
            );
        }
    */
    /*@Test
    public void testFree(){

        //Given
        Event event = Event.builder()
                .basePrice(0)
                .maxPrice(0)
                .build();
        //When
        event.update();

        //Then
        assertThat(event.isFree()).isTrue();

        //Given
        event = Event.builder()
                .basePrice(100)
                .maxPrice(0)
                .build();
        //When
        event.update();

        //Then
        assertThat(event.isFree()).isFalse();

        //Given
        event = Event.builder()
                .basePrice(0)
                .maxPrice(100)
                .build();
        //When
        event.update();

        //Then
        assertThat(event.isFree()).isFalse();
    }*/
    @ParameterizedTest(name = "{index} 번 테스트 location={0}, isOffline={1}")
    @MethodSource("paramsForTestOffline")
    public void testOffline(String location, boolean isOffline){
        //Given
        Event event = Event.builder()
                .location(location)
                .build();
        //When
        event.update();

        //Then
        assertThat(event.isOffline()).isEqualTo(isOffline);
    }
    private static Object[] paramsForTestOffline(){
        return new Object[]{
                new Object[] {"강남", true},
                new Object[] {null, false},
                new Object[] {"       ", false},
        };
    }
    /*@Test
    public void testOffline(){
        //Given
        Event event = Event.builder()
                .location("강남역 네이버 D2 스타텁 팩토리")
                .build();
        //When
        event.update();

        //Then
        assertThat(event.isOffline()).isTrue();

        //Given
        event = Event.builder()
                .build();
        //When
        event.update();

        //Then
        assertThat(event.isOffline()).isFalse();
    }*/
}