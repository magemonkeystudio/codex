import os
import re
import sys

is_dev = len(sys.argv) >= 2 and eval(sys.argv[1].lower().capitalize())
prep = not is_dev and len(sys.argv) >= 3 and bool(sys.argv[2])


def replace_version(pom_path):
    regex = r'(<artifactId>.*codex.*<\/artifactId>\s*)<version>((((\d+\.?)+)((-R(\d+)\.?)(\d+)?)?)(-SNAPSHOT)?)<\/version>$'
    with open(pom_path, 'r') as pom:
        contents = pom.read()
        ver = re.findall(regex, contents, re.MULTILINE)
        version = ver[0][1]
        bare_version = ver[0][3]
        if is_dev:
            if not '-R' in version:
                new_version = version + '-R0.1-SNAPSHOT'
            elif not '-SNAPSHOT' in version:
                new_version = version + '.1-SNAPSHOT'
            else:
                r_version = ver[0][6]
                patch = int(ver[0][8]) + 1
                new_version = bare_version + r_version + str(patch) + '-SNAPSHOT'
        elif prep:
            r_version = int(ver[0][7]) + 1
            new_version = bare_version + '-R' + str(r_version)
        else:
            version = ver[0][3]
            minor = int(ver[0][4])
            new_version = version[:-(len(str(minor)))] + str(minor + 1)
        contents = re.sub(regex,
                          ver[0][0] + '<version>' + new_version + '</version>',
                          contents,
                          1,
                          re.MULTILINE)
    with open(pom_path, 'w') as pom:
        pom.write(contents)


def find_pom_files(directory):
    pom_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file == 'pom.xml':
                pom_files.append(os.path.join(root, file))
    return pom_files

if __name__ == "__main__":
    directory = os.getcwd()
    pom_files = find_pom_files(directory)
    for pom_file in pom_files:
        # NMS versions should be updated manually when NMS is actually changed
        if '-nms' in pom_file: continue
        print(f'Updating version in {pom_file}')
        replace_version(pom_file)
