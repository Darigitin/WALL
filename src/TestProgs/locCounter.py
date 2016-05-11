#!/usr/bin/python3
# ******************************************************
# * Program: LOC Counter
# * 
# * Purpose: Count lines of code.
# *          Count lines of comments.
# *          *For Java files only.
# * 
# * @author: jl948836, Jordan Lescallette
# * 
# * date/ver: 04/11/16 1.0.0 
# ******************************************************

import re, os

def header():
    print("I count.")
    print("Let me do my job.")

'''
    Lines of Code
    Skip whitespace; condense '{' and '}'
'''
def loCode(fName, file):
    loc = 0
    blockComment = False
    for line in file:
        line = re.split("\s+", line)
        if blockComment:
            blockComment = "*/" not in line
        elif len(line) > 1:
            if line[1].startswith(("/*","/**")):
                blockComment = True
            elif "//" not in line[1]:
                loc += 1

    print(fName + " Number of Lines of Code: ", loc)
    return loc

'''
    Lines of Comments
    Count Block Comments and in-line Comments
'''
def loComments(fName, file):
    blockComment = False
    javaDoc = False
    comments = 0
    prologues = 0
    for line in file:
        line = re.split("\s+",line)
        if blockComment:
            blockComment = "*/" not in line
            comments += 1
        elif "/**" in line or "/*" in line:
            blockComment = True
            comments += 1
        elif "//" in line:
            comments += 1
        # if blockComment:
        #     blockComment = "*/" not in line
        #     comments += 1
        # elif javaDoc:
        #     javaDoc = "*/" not in line
        #     prologues += 1
        # elif "/**" in line:
        #     javaDoc = True
        #     prologues += 1
        # elif "/*" in line:
        #     blockComment = True
        #     comments += 1
        # elif "//" in line:
        #     comments += 1
    print(fName + " Number of Comments: ", comments)#, " - Number of javaDoc Comments: ", prologues)

    return comments#, prologues

''' 
    Prompt the user to work on a single file or a directory, then get the path
    and/or directory that they want to work on.
'''
def parms():
    path = ""
    pathList = []
    options = input("1 - Single File | 2 - Directory:\n")
    if options == "1":
        path = input("Enter the files path (include final '/', not file name): ")
        pathList.append(input("Enter the name of the file (include the extension): "))
    elif options == "2":
        path = input("Enter the path of the directory with the files you wish to count (include final '/'):\n")
        pathList = os.listdir(path)
    return path, pathList

'''
    Loop until the user decides to quit. Open the files print the total loc and
    number of comments.
'''
def main():
    header()

    q = False
    while not q:
        path, files = parms()
        comments = 0
        javaDoc = 0
        loc = 0
        for fName in files:
            if ".java" in fName:
                file = open(path + fName, "r")
                file = file.readlines()

                comments += loComments(fName,file)
                #temp = loComments(fName, file)
                #comments += temp[0]
                #javaDoc += temp[1]

                loc += loCode(fName, file)
        print("Total number of comments: ", comments)
        print("Total lines of code: ", loc)
        answer = input("Do you want to quit? (yes or no): ")
        if answer == "yes":
            q = True

main()