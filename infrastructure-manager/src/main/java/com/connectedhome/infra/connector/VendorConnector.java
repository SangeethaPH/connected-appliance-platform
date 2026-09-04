package com.connectedhome.infra.connector;

import com.connectedhome.infra.entity.VendorEntity;

public interface VendorConnector {
    VendorApplianceSnapshot fetchAppliance(VendorEntity vendor, String externalApplianceId);
}
