package jkbstudio.parameter;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameterListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A "group" proxy parameter. When enabled and the internal parameter value is changed, the new
 * value is propagated to all child parameters.  If a child parameter is changed by the user,
 * the Enabled value becomes False to indicate an out-of-sync state.
 */
public class SyncParameter extends LXComponent {

  public final BooleanParameter enabled =
    new BooleanParameter("Enabled", true)
      .setDescription("Indicates whether changes to this parameter will be pushed to child parameters");

  public final LXListenableNormalizedParameter parameter;

  private final List<LXListenableNormalizedParameter> childParams = new ArrayList<>();

  /**
   * Constructor
   * @param lx LX instance
   * @param parameter Lead parameter. All child parameters should match this type.
   */
  public SyncParameter(LX lx, LXListenableNormalizedParameter parameter) {
    super(lx);
    this.parameter = Objects.requireNonNull(parameter);
    this.parameter.addListener(this.internalChanged);
    this.enabled.addListener(this.enabledChanged);

    addParameter("sync", this.enabled);
    addParameter("parameter", this.parameter);
  }

  public SyncParameter addChildParameter(LXListenableNormalizedParameter child) {
    if (this.childParams.contains(Objects.requireNonNull(child))) {
      throw new IllegalArgumentException("Child parameter already exists in collection: " + child);
    }
    this.childParams.add(child);
    if (this.enabled.isOn()) {
      child.setNormalized(this.parameter.getNormalized());
    }
    child.addListener(this.childChanged);
    return this;
  }

  public boolean removeChildParameter(LXListenableNormalizedParameter child) {
    if (this.childParams.remove(Objects.requireNonNull(child))) {
      child.removeListener(this.childChanged);
      return true;
    }
    return false;
  }

  private boolean isUs;

  private LXParameterListener enabledChanged = (p) -> {
    if (!this.isUs && this.enabled.isOn()) {
      // Sync was turned on. Push internal value to children.
      this.isUs = true;
      pushToChildren();
      this.isUs = false;
    }
  };

  private LXParameterListener internalChanged = (p) -> {
    // If parameter was modified and sync is turned on, push to child params
    if (!this.isUs && this.enabled.isOn()) {
      this.isUs = true;
      pushToChildren();
      this.isUs = false;
    }
  };

  private void pushToChildren() {
    double normalized = this.parameter.getNormalized();
    for (LXListenableNormalizedParameter child : this.childParams) {
      child.setNormalized(normalized);
    }
  }

  private LXParameterListener childChanged = (p) -> {
    // If child was changed not by us, turn off sync
    if (!this.isUs) {
      this.isUs = true;
      this.enabled.setValue(false);
      this.isUs = false;
    }
  };

  @Override
  public void dispose() {
    for (int i = this.childParams.size() - 1; i >= 0; i--) {
      LXListenableNormalizedParameter child = this.childParams.get(i);
      removeChildParameter(child);
    }
    this.parameter.removeListener(this.internalChanged);
    this.enabled.removeListener(this.enabledChanged);
    this.parameter.dispose();
    super.dispose();
  }
}
