package com.cyclist.UI;

public interface OnNewInstruction {
    void setInstructionBar(InstructionsFragment fragment);
    void setRouteDetailsBar(RouteDetailsFragment fragment);
    void hideRoutingFragments();
    void showRoutingFragments();
}
