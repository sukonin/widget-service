package com.miro.widgetservice.model;

import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "WIDGETS",
    indexes = {@Index(
        name = "COORDINATE_INDEX",
        columnList = "xPoint,yPoint,xPoint2,yPoint2")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class WidgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer xPoint;

    @Column(nullable = false)
    private Integer yPoint;

    @Column(nullable = false, unique = true)
    private Integer zIndex;

    @Column(nullable = false)
    private Integer xPoint2;

    @Column(nullable = false)
    private Integer yPoint2;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(nullable = false)
    private LocalDateTime modificationDate;

    @PrePersist
    public void prePersist() {
        prepareEntity();
    }

    @PreUpdate
    public void preUpdate() {
        prepareEntity();
    }

    private void prepareEntity() {
        setXPoint2(getXPoint() + getWidth());
        setYPoint2(getXPoint() + getWidth());
        setModificationDate(LocalDateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WidgetEntity)) {
            return false;
        }
        WidgetEntity that = (WidgetEntity)o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
