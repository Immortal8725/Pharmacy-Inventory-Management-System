using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

internal static class HealthFirstLauncher
{
    [STAThread]
    private static int Main(string[] args)
    {
        try
        {
            string baseDir = AppDomain.CurrentDomain.BaseDirectory;
            string jar = Path.Combine(baseDir, "dist", "healthfirst_pims.jar");
            if (!File.Exists(jar))
            {
                jar = Path.Combine(baseDir, "healthfirst_pims.jar");
            }
            if (!File.Exists(jar))
            {
                MessageBox.Show(
                    "Could not find dist\\healthfirst_pims.jar next to this executable.\n\n" +
                    "Keep healthfirst_pims.exe in the project folder (alongside the dist folder).",
                    "HealthFirst PIMS",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                return 1;
            }

            string java = FindJava();
            if (java == null)
            {
                MessageBox.Show(
                    "Java was not found.\n\nInstall JDK 17 or newer, then try again.",
                    "HealthFirst PIMS",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                return 1;
            }

            string arguments = "-jar \"" + jar + "\"";
            foreach (string arg in args)
            {
                arguments += " " + Quote(arg);
            }

            var start = new ProcessStartInfo
            {
                FileName = java,
                Arguments = arguments,
                WorkingDirectory = baseDir,
                UseShellExecute = false
            };

            using (Process process = Process.Start(start))
            {
                if (process == null)
                {
                    MessageBox.Show("Failed to start HealthFirst PIMS.", "HealthFirst PIMS",
                        MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return 1;
                }
                process.WaitForExit();
                return process.ExitCode;
            }
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.Message, "HealthFirst PIMS", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }
    }

    private static string FindJava()
    {
        string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrWhiteSpace(javaHome))
        {
            string homeJava = Path.Combine(javaHome, "bin", "javaw.exe");
            if (File.Exists(homeJava))
            {
                return homeJava;
            }
        }

        string programFiles = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
        string[] searchRoots =
        {
            Path.Combine(programFiles, "Java"),
            Path.Combine(programFiles, "Eclipse Adoptium"),
            Path.Combine(programFiles, "Microsoft"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "Java")
        };

        foreach (string root in searchRoots)
        {
            if (!Directory.Exists(root))
            {
                continue;
            }
            foreach (string dir in Directory.GetDirectories(root))
            {
                string candidate = Path.Combine(dir, "bin", "javaw.exe");
                if (File.Exists(candidate))
                {
                    return candidate;
                }
            }
        }

        return FindOnPath("javaw.exe") ?? FindOnPath("java.exe");
    }

    private static string FindOnPath(string fileName)
    {
        string pathEnv = Environment.GetEnvironmentVariable("PATH") ?? "";
        foreach (string dir in pathEnv.Split(Path.PathSeparator))
        {
            if (string.IsNullOrWhiteSpace(dir))
            {
                continue;
            }
            string candidate = Path.Combine(dir.Trim('"'), fileName);
            if (File.Exists(candidate))
            {
                return candidate;
            }
        }
        return null;
    }

    private static string Quote(string value)
    {
        if (string.IsNullOrEmpty(value))
        {
            return "\"\"";
        }
        if (value.IndexOfAny(new[] { ' ', '\t', '"' }) < 0)
        {
            return value;
        }
        return "\"" + value.Replace("\"", "\\\"") + "\"";
    }
}
