package sep.server.model.game;

/**
 * @param in        The in time of the request.
 * @param register  The register to which the privilege mask belongs.
 */
public final record RAdminPrivilegeMask(long in, int register)
{
}
