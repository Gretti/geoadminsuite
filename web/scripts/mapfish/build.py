import os
import sys
from shutil import copy2
from popen2 import Popen4

def copytree(src, dst, symlinks=0, skip=[]):
    """Recursively copy a directory tree using copy2() (from shutil.copytree.)
    Added a `skip` parameter consisting of names which we don't want to copy. """
    names = os.listdir(src)
    os.mkdir(dst)
    for name in names:
        if name in skip:
            continue
        srcname = os.path.join(src, name)
        dstname = os.path.join(dst, name)
        try:
            if symlinks and os.path.islink(srcname):
                linkto = os.readlink(srcname)
                os.symlink(linkto, dstname)
            elif os.path.isdir(srcname):
                copytree(srcname, dstname, symlinks, skip)
            else:
                copy2(srcname, dstname)
        except (IOError, os.error), why:
            print "Can't copy %s to %s: %s" % (`srcname`, `dstname`, str(why))

if __name__ == "__main__":
    cd = os.getcwd()
    pd = os.path.normpath(os.path.join(cd, os.path.pardir))

    # build MapFish
    os.chdir(os.path.join(cd, "build"))
    output = os.path.join(pd, "refexportfiles", "mapfish", "MapFish.js")
    cfgfil = os.path.join(cd, "gas.cfg")
    cmd = "python build.py -o " + output + " -c " + cfgfil
    p = Popen4(cmd, True)
    for line in p.fromchild.readlines():
        sys.stdout.write(line)
    ret = p.wait()
    if ret != 0:
        sys.stderr.write("build.py subprocess failed\n")
    os.chdir(cd)

    # copy MapFish img
    #src = os.path.join(cd, "mapfish", "mfbase", "mapfish", "img")
    #dst = os.path.join(pd, "refexportfiles", "mapfish")

    sys.exit(0)
