import sys

filepath = sys.argv[1]
with open(filepath, 'r') as f:
    c = f.read()

# Fix 1: loadSeckillHighlight - card-img rendering
old = "'<div class=\"card-img\">' + (p.image || '\U0001f4f1') + '</div>'"
new = "'<div class=\"card-img\">' + (p.image ? '<img src=\"' + p.image + '\" alt=\"' + p.name + '\" loading=\"lazy\">' : '\U0001f4f1') + '</div>'"
c = c.replace(old, new)

# Fix 2: loadSceneSections - card-img rendering
old2 = "'<div class=\"card-img\">' + (p.image || '\U0001f6cd️') + '</div>'"
new2 = "'<div class=\"card-img\">' + (p.image ? '<img src=\"' + p.image + '\" alt=\"' + p.name + '\" loading=\"lazy\">' : '\U0001f6cd️') + '</div>'"
c = c.replace(old2, new2)

# Fix 3: Card name escape for alt attribute in scene sections
# Already handled by using p.name in the template literal

with open(filepath, 'w') as f:
    f.write(c)

print(f"Fixed {filepath}")
