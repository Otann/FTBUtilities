package latmod.ftbu.mod.cmd;

import latmod.ftbu.cmd.*;
import latmod.ftbu.mod.cmd.admin.*;
import latmod.ftbu.mod.config.FTBUConfigGeneral;

public class CmdAdmin extends CommandSubLM
{
	public CmdAdmin()
	{
		super(FTBUConfigGeneral.commandAdmin.get(), CommandLevel.OP);
		add(new CmdAdminPlayer("player"));
		add(new CmdAdminReload("reload"));
		add(new CmdAdminSetItemName("setitemname"));
		add(new CmdAdminGetDim("getdim"));
		add(new CmdAdminInvsee("invsee"));
		add(new CmdAdminSetWarp("setwarp"));
		add(new CmdAdminWorldBorder("worldborder"));
		add(new CmdAdminSpawnArea("spawnarea"));
		add(new CmdAdminUnclaim("unclaim"));
		add(new CmdAdminBackup("backup"));
		add(new CmdAdminGetmode("getmode"));
		add(new CmdAdminSetmode("setmode"));
		add(new CmdAdminConfig("config"));
	}
}