package predix.psql.model;

import com.google.common.base.MoreObjects;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Student {
  @Min(1)
  private Integer id;
  @NotNull
  @Size(min = 1, max = 20)
  private String name;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass().getName()).add("id", id).add("name", name).toString();
  }
}
