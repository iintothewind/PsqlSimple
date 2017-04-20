package predix.psql.api;

import com.github.davidmoten.rx.jdbc.ConnectionProvider;
import com.github.davidmoten.rx.jdbc.Database;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Min;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/student")
public class Student {
  private static final Logger log = LoggerFactory.getLogger(Student.class);
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
        .toBlocking().single()).map(r -> ResponseEntity.ok(ImmutableMap.of("student", (Object) r)))
      .getOrElse(ResponseEntity.notFound().build());
  }

  @RequestMapping(path = "/all", method = RequestMethod.GET)
  public ResponseEntity<? extends Map<String, Object>> all() {
    return ResponseEntity.ok(ImmutableMap.of("students", Try.of(() ->
      db.select("select id, name from student")
        .getAs(Integer.class, String.class)
        .map(t -> ImmutableMap.of("id", t._1(), "name", t._2()))
        .toBlocking().toIterable())
      .getOrElse(ImmutableList.of())));

  }

  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<? extends Map<String, Object>> handleException(ConstraintViolationException e) {
    return ResponseEntity.badRequest().body(ImmutableMap.of("error", "validation error"));
  }
}
