package predix.psql.api;

import com.github.davidmoten.rx.jdbc.ConnectionProvider;
import com.github.davidmoten.rx.jdbc.Database;
import com.google.common.collect.ImmutableMap;
import javaslang.Tuple;
import javaslang.control.Option;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import predix.psql.model.Student;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Map;

import static javaslang.API.*;

@Validated
@RestController
@RequestMapping("/student")
public class StudentApi {
  private static final Logger log = LoggerFactory.getLogger(StudentApi.class);
  private Database db;

  @Resource(name = "connectionProvider")
  private ConnectionProvider connectionProvider;

  @PostConstruct
  public void init() {
    db = Database.from(connectionProvider);
  }

  @RequestMapping(path = "/{id}", method = RequestMethod.GET)
  public ResponseEntity<? extends Map<String, Object>> one(@Min(value = 1) @PathVariable(value = "id") Integer id) {
    return Try.of(() ->
      db.select("select id, name from student where id = :id")
        .parameter("id", id)
        .getAs(Integer.class, String.class)
        .map(t -> ImmutableMap.of("id", t._1(), "name", t._2()))
        .toBlocking().single())
      .map(r -> ResponseEntity.ok(ImmutableMap.of("student", (Object) r)))
      .getOrElse(ResponseEntity.notFound().build());
  }

  @RequestMapping(path = "/all", method = RequestMethod.GET)
  public ResponseEntity<? extends Map<String, Object>> all() {
    return Try.of(() -> db.select("select id, name from student")
      .getAs(Integer.class, String.class)
      .map(t -> ImmutableMap.of("id", t._1(), "name", t._2()))
      .toBlocking().toIterable())
      .map(lst -> ResponseEntity.ok(new ImmutableMap.Builder<String, Object>().put("students", lst).build()))
      .getOrElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
  }

  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<? extends Map<String, Object>> create(@Valid @RequestBody Student student) {
    log.info("student = {}", student);
    return Try.of(() -> db.update("insert into student(name) values (:name)")
      .parameter("name", student.getName())
      .returnGeneratedKeys()
      .getAs(Integer.class)
      .toBlocking().single())
      .map(v -> ResponseEntity.ok(new ImmutableMap.Builder<String, Object>().put("created", ImmutableMap.of("id", v)).build()))
      .getOrElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
  }

  @RequestMapping(path = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<? extends Map<String, Object>> update(@Min(value = 1) @PathVariable(value = "id") Integer id, @Valid @RequestBody Student student) {
    log.info("id = {}, student = {}", id, student);
    return Match(Tuple.of(id, student)).of(
      Case($(t -> Option.of(t._2.getId()).isDefined() && one(t._1).getStatusCode().is2xxSuccessful()), v -> Try.of(() -> db.update("update student set id = :student_id, name = :student_name where id = :id")
        .parameter("student_id", v._2.getId())
        .parameter("student_name", v._2.getName())
        .parameter("id", v._1)
        .count()
        .toBlocking().single())
        .map(r -> ResponseEntity.ok(new ImmutableMap.Builder<String, Object>().put("updated", ImmutableMap.of("id", v._1)).build()))
        .getOrElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())),
      Case($(t -> Option.of(t._2.getId()).isDefined() && t._1.equals(t._2.getId())), v -> Try.of(() -> db.update("insert into student(id, name) values (:id, :name)")
        .parameter("id", v._2.getId())
        .parameter("name", v._2.getName())
        .returnGeneratedKeys()
        .getAs(Integer.class)
        .toBlocking().single())
        .map(k -> ResponseEntity.ok(new ImmutableMap.Builder<String, Object>().put("created", ImmutableMap.of("id", k)).build()))
        .getOrElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())),
      Case($(), t -> ResponseEntity.badRequest().body(new ImmutableMap.Builder<String, Object>().put("error", "bad request").build()))
    );
  }


  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<? extends Map<String, Object>> handleException(ConstraintViolationException e) {
    return ResponseEntity.badRequest().body(ImmutableMap.of("error", "validation error"));
  }
}
