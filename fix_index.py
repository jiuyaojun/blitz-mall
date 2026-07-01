#!/usr/bin/env python3
"""Fix image rendering in index.html - use <img> tags instead of text URLs"""

filepath = '/home/ubuntu/blitz/blitz-server/src/main/resources/static/index.html'

with open(filepath, 'r') as f:
    content = f.read()

# Fix 1: seckill highlight section - render image as <img> tag
old = "'<div class=\"card-img\">' + (p.image || '\U0001f4f1') + '</div>'"
new = "'<div class=\"card-img\">' + (p.image ? '<img src=\"' + p.image + '\" alt=\"' + p.name + '\">' : '\U0001f4f1') + '</div>'"
if old in content:
    content = content.replace(old, new)
    print('Fix 1 applied (seckill highlight)')
else:
    print('Fix 1: pattern not found')

# Fix 2: scene sections - render image as <img> tag
old2 = "'<div class=\"card-img\">' + (p.image || '\U0001f6cd️') + '</div>'"
new2 = "'<div class=\"card-img\">' + (p.image ? '<img src=\"' + p.image + '\" alt=\"' + p.name + '\">' : '\U0001f6cd️') + '</div>'"
if old2 in content:
    content = content.replace(old2, new2)
    print('Fix 2 applied (scene sections)')
else:
    # Try alternative pattern
    old2b = "'<div class=\"card-img\">' + (p.image || '\U0001f6cd') + '</div>'"
    if old2b in content:
        content = content.replace(old2b, new2)
        print('Fix 2b applied (scene sections alt)')
    else:
        print('Fix 2: pattern not found, searching...')
        for i, line in enumerate(content.split('\n')):
            if 'card-img' in line and ('p.image' in line or 'emoji' in line.lower()):
                print(f'  Line {i+1}: {line.strip()[:80]}')

with open(filepath, 'w') as f:
    f.write(content)
print('Done')
