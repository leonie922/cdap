package com.continuuity.gradle
/**
 * Created with IntelliJ IDEA.
 * User: Eric
 * Date: 5/26/12
 * Time: 8:35 PM
 * To change this template use File | Settings | File Templates.
 */
class GitRepo {

    private String name;

    private String branch;

    private String origin;

    private String dir;

    private String shell;

    private String shellOption;

    public update ()
    {
        init();
        File target = new File(dir);
        if(!target.exists())
        {
            cloneRepo();
        }
    }

    public void cloneRepo ()
    {
        init();
        File file = new File(dir);
        String cmd = "git clone -v --branch $branch $origin $file.canonicalPath";
        println "Cloning $name ($cmd)...";
        runCommand(cmd);
    }

    private void runCommand (String cmd)
    {
        ProcessBuilder processBuilder = new ProcessBuilder(shell, shellOption, cmd)
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();
        BufferedReader cmdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = ""
        while((line = cmdOut.readLine()) != null)
        {
            println line;
        }
        if(p.waitFor() != 0)
        {
            println "Command failed: $cmd";
            throw new Exception("Command failed: $cmd");
        }
    }

    private void init ()
    {
        if(name == null)
        {
            throw new IllegalArgumentException("repoName is a required.")
        }

        if(branch == null)
        {
            branch = "master"
        }

        if(origin == null)
        {
            origin = String.format("git@github.com:continuuity/%s.git", name);
        }

        if(dir == null)
        {
            dir = name;
        }

        if (shell == null)
        {
            if (System.properties['os.name'].toLowerCase().contains('windows'))
            {
                shell = "cmd.exe"
                shellOption = "/c"
            }
            else
            {
                shell = "/bin/bash"
                shellOption = "-c"
            }
        }

    }
}
