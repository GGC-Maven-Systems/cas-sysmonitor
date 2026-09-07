package ph.com.guanzongroup.cas.sysmonitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Maynard 2026/08/26
 */
public class StockRequestApproval implements iSystemMonitor {

    private String psMonitorName = "Pending Bills";
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


        lsSQL = "SELECT"
                + "  d.sClustrID"
                + ",  a.sTransNox"
                + ",  a.dtransact"
                + ",  a.sBranchCd"
                + ",  e.sBranchNm"
                + ",  a.sIndstCdx"
                + ",  a.sCategrCd"
                + ",  a.sTransNox"
                + ",  b.nQuantity"
                + ",  b.nApproved"
                + ",  b.nCancelld"
                + ",  b.nIssueQty"
                + ",  b.nOrderQty"
                + ",  b.nReceived" 
                + ", CONCAT(a.sTransNox ,' - ',a.dTransact) sDisplayNme"
                + ", CONCAT(e.`sBranchNm`, ' - #',d.`sClustrDs`) sToolTipx"
                + " FROM Inv_Stock_Request_Master a"
                + "  LEFT JOIN Inv_Stock_Request_Detail b ON a.sTransNox = b.sTransNox"
                + "  LEFT JOIN Branch_Others c ON a.sBranchCD = c.sBranchCd"
                + "  LEFT JOIN Branch_Cluster d ON c.sClustrID = d.sClustrID"
                + "  LEFT JOIN Branch e ON a.sBranchCD = e.sBranchCd"
                + "     WHERE "
                + "         ("
                + "              b.nApproved > 0"
                + "             AND b.nApproved > ( b.nCancelld + b.nIssueQty + b.nOrderQty )"
                + "         )"
                + "          AND (a.cProcessd = '1')"
                + "          AND (a.cTranStat = '1')";
        
        String lsFilterAll = "";
        String lsFilter;

//        set filter by industry
        lsFilter = "";
        if (pasIndstCdx != null) { //Never pang na lagyan ito pasIndstCdx ng value; as per sir maynard kasi ni as is palang muna yung pag filter dapat mag filter pa sa lahat ng industry;
            for (String lsValue : pasIndstCdx) {
                lsFilter += ", " + SQLUtil.toSQL(lsValue);
            }
        }
        if (!lsFilter.isEmpty()) {
            lsFilterAll += " AND c.sIndstCdx IN(" + lsFilter.substring(2) + ")";
        }
        lsFilter = "";

        //Filter by Company based of current logged in; filter by branch company - request by ma'am she : Arsiela 05-28-2026 
        if (pasCompnyID != null) {
            for (String lsValue : pasCompnyID) {
                lsFilter += ", " + SQLUtil.toSQL(lsValue);
            }
        }
        if (!lsFilter.isEmpty()) {
            lsFilterAll += " AND a.sCompnyID IN(" + lsFilter.substring(2) + ")";
        }

        if (!lsFilterAll.isEmpty()) {
            lsSQL += lsFilterAll;
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox ORDER BY a.dTransact ASC  "; 
        try {
            System.out.println("Monitoring Query is = " + lsSQL);
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
