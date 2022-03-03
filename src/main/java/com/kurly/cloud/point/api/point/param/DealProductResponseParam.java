package com.kurly.cloud.point.api.point.param;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor(staticName = "create")
public class DealProductResponseParam {
    private long dealProductNo;
    @Setter
    private int usedFreePoint;
    private int usedPaidPoint;

    public void increaseUseFreePoint(){ usedFreePoint+=1;}
}
