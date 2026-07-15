
package ph.com.guanzongroup.cas.sysmonitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Administrator
 */
public class UnPaidPRF implements iSystemMonitor {

    private String psMonitorName = "UnPaid PRF";
    private GRiderCAS poDriver;
    private String[] pasBranchCD;
    private String[] pasCompnyID;
    private String[] pasIndstCdx;
    private String[] pasCategrCd;

    JSONArray poJAData = null;

    @Override
    public void setDriver(GRiderCAS driver) {
        poDriver = driver;
    }

    @Override
    public String getName() {
        return psMonitorName;
    }

    @Override
    public void setBranchFilter(String[] branchcd) {
        pasBranchCD = branchcd;
    }

    @Override
    public void setCompanyFilter(String[] companycd) {
        pasCompnyID = companycd;
    }

    @Override
    public void setIndustryFilter(String[] indstcd) {
        pasIndstCdx = indstcd;
    }

    @Override
    public void setCategoryFilter(String[] categcd) {
        pasCategrCd = categcd;
    }

    @Override
    public JSONObject processMonitor() {
        String lsSQL;
        JSONObject oRes = new JSONObject();

        pasBranchCD = new String[]{poDriver.getBranchCode()};
        lsSQL = "SELECT"
                + "  a.sTransNox"
                + ", a.dTransact"
                + ", c.sPayeeNme"
                + ", b.sBranchNm"
                + ", d.sCompnyNm"
                + ", a.sIndstCdx"
                + ", a.cProcessd"
                + ", a.cTranStat"
                + ", CONCAT(a.sTransNox ,' - ',a.dTransact) sDisplayNme"
                + ", CONCAT(b.`sBranchNm`, ' - #',a.`sTransNox`) sToolTipx"
                + " FROM Payment_Request_Master a"
                    + " LEFT JOIN Branch b ON a.sBranchCd = b.sBranchCD"
                    + " LEFT JOIN Payee c ON a.sPayeeIDx = c.sPayeeIDx"
                    + " LEFT JOIN Company d ON a.sCompnyID = d.sCompnyID"
                + " WHERE a.cTranStat IN ('1')"
                + " AND a.nNetTotal > a.nAmtPaidX ";

        String lsFilterAll = "";
        String lsFilter;

        //set filter by industry
        lsFilter = "";
        if (pasIndstCdx != null) {
            for (String lsValue : pasIndstCdx) {
                lsFilter += ", " + SQLUtil.toSQL(lsValue);
            }
        }
        if (!lsFilter.isEmpty()) {
            lsFilterAll += " AND a.sIndstCdx IN(" + lsFilter.substring(2) + ")";
        }
 
        //set filter by company
        lsFilter = "";
        if (pasCompnyID != null) {
            for (String lsValue : pasCompnyID) {
                lsFilter += ", " + SQLUtil.toSQL(lsValue);
            }
        }
        if (!lsFilter.isEmpty()) {
            lsFilterAll += " AND a.sCompnyID IN(" + lsFilter.substring(2) + ")";
        }

        //set filter by branch
        lsFilter = "";
        if (pasBranchCD != null) {
            for (String lsValue : pasBranchCD) {
                lsFilter += ", " + SQLUtil.toSQL(lsValue);
            }
        }
        if (!lsFilter.isEmpty()) {
            lsFilterAll += " AND LEFT(a.sTransNox,4) IN(" + lsFilter.substring(2) + ")";
        }

        if (!lsFilterAll.isEmpty()) {
            lsSQL += lsFilterAll;
        }

        try {
//            System.out.println("Monitoring Query is = " + lsSQL);
            lsSQL = lsSQL + " ORDER BY a.dTransact ASC ";
            ResultSet loRS = poDriver.executeQuery(lsSQL);

            poJAData = MiscUtil.RS2JSON(loRS);

        } catch (SQLException ex) {
            oRes.put("result", "Failed");
            oRes.put("message", MiscUtil.getException(ex));
            return oRes;
        }

        oRes.put("result", "Success");
        return oRes;
    }

    @Override
    public JSONArray getRecords() {
        return poJAData;
    }

}
