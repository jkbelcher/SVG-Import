package studio.jkb.parameter;

import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.MutableParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Performs a summary operation on all child parameter values.
 * Could generalize this to [perform function] on child parameters.
 */
public class SumParameter extends MutableParameter {

  private final List<LXListenableParameter> mutableParameters = new ArrayList<>();
  protected final List<LXListenableParameter> parameters = Collections.unmodifiableList(this.mutableParameters);

  public SumParameter(String label) {
    super(label);
  }

  @Override
  public SumParameter setDescription(String description) {
    super.setDescription(description);
    return this;
  }

  public SumParameter addChildParameter(LXListenableParameter parameter) {
    if (this.mutableParameters.contains(Objects.requireNonNull(parameter))) {
      throw new IllegalArgumentException("Child parameter already exists in collection: " + parameter);
    }
    this.mutableParameters.add(parameter);
    parameter.addListener(this.childChanged);
    refresh();
    return this;
  }

  public boolean hasChildParameter(LXListenableParameter parameter) {
    return this.parameters.contains(parameter);
  }

  public boolean removeChildParameter(LXListenableParameter parameter) {
    if (this.mutableParameters.remove(Objects.requireNonNull(parameter))) {
      parameter.removeListener(this.childChanged);
      refresh();
      return true;
    }
    return false;
  }

  private LXParameterListener childChanged = (p) -> {
    refresh();
  };

  private void refresh() {
    setValue(computeGroup(this.parameters));
  }

  /**
   * Perform a summary operation on the collection, such as sum/min/max.
   */
  protected double computeGroup(List<LXListenableParameter> parameters) {
    double value = 0;
    for (LXListenableParameter child : parameters) {
      value += child.getValue();
    }
    return value;
  }

  @Override
  public final double getValue() {
    return super.getValue();
  }

  @Override
  public void dispose() {
    for (int i = this.mutableParameters.size() - 1; i >= 0; i--) {
      removeChildParameter(this.mutableParameters.get(i));
    }
    super.dispose();
  }
}
