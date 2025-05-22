package com.itesoft.registree.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.validation.annotation.Validated;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, defaultImpl = GroupRegistry.class)
@Validated
public class GroupRegistry extends Registry {
  @NotEmpty
  private List<String> memberNames;

  public List<String> getMemberNames() {
    return memberNames;
  }

  public void setMemberNames(final List<String> memberNames) {
    this.memberNames = memberNames;
  }
}
