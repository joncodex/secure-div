package com.enzelascripts.securediv.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@DiscriminatorValue("TRANSCRIPT")
@Getter
@Setter
//@SuperBuilder
//@NoArgsConstructor
//@AllArgsConstructor
//@EqualsAndHashCode(callSuper = true)
public class Transcript extends Document {

}