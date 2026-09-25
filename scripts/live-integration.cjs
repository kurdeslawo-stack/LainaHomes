const fs = require('node:fs')
const mineflayer = require('mineflayer')
const minecraftDataApi = require('minecraft-data')

const minecraftData = minecraftDataApi('26.1')
minecraftData.version.version = 776
minecraftDataApi.versionsByMinecraftVersion.pc['26.1'].version = 776

const stage = process.argv[2] || 'before-restart'
const results = []
const messages = []
const bot = mineflayer.createBot({
  host: '127.0.0.1',
  port: 25565,
  username: 'BobUX',
  auth: 'offline',
  version: '26.1'
})

bot.on('messagestr', message => {
  messages.push(message)
  process.stdout.write(`[BobUX] ${message}\n`)
})

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms))

function record (name, passed, detail) {
  results.push({ name, passed, detail })
  process.stdout.write(`${passed ? 'PASS' : 'FAIL'} ${name}: ${detail}\n`)
}

function waitForSpawn () {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error('Brak spawnu BobUX')), 30000)
    bot.once('spawn', () => {
      clearTimeout(timer)
      resolve()
    })
    bot.once('error', reject)
  })
}

async function command (text, delay = 350) {
  bot.chat(text)
  await sleep(delay)
}

async function openGui (commandText) {
  if (bot.currentWindow) bot.closeWindow(bot.currentWindow)
  const opened = new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error(`GUI nie otworzyło się po ${commandText}`)), 5000)
    bot.once('windowOpen', window => {
      clearTimeout(timer)
      resolve(window)
    })
  })
  bot.chat(commandText)
  return opened
}

function itemName (window, slot) {
  return window.slots[slot]?.name || null
}

function titleText (window) {
  return JSON.stringify(window?.title || '')
}

async function beforeRestart () {
  if (stage === 'resume-before-restart') {
    const before = bot.entity.position.clone()
    await command('/home lh_beta', 6500)
    record('/home <name> pozostaje EssentialsX', bot.entity.position.distanceTo(before) > 2,
      `zmiana pozycji=${bot.entity.position.distanceTo(before).toFixed(2)}`)
    for (let index = 22; index < 29; index++) {
      await command(`/sethome lh${String(index).padStart(2, '0')}`, 1200)
    }
  } else {
    const alpha = bot.entity.position.clone()
    await command('/sethome lh_alpha')
    bot.setControlState('forward', true)
    await sleep(1800)
    bot.setControlState('forward', false)
    await command('/sethome lh_beta')
    const moved = bot.entity.position.clone()

    await command('/home lh_alpha', 6500)
    record('/home <name> pozostaje EssentialsX', bot.entity.position.distanceTo(alpha) < 2 && moved.distanceTo(alpha) > 2,
      `odległość po ruchu=${moved.distanceTo(alpha).toFixed(2)}, po /home=${bot.entity.position.distanceTo(alpha).toFixed(2)}`)

    for (let index = 0; index < 29; index++) {
      await command(`/sethome lh${String(index).padStart(2, '0')}`, 1200)
    }
  }

  let window = await openGui('/home')
  record('/home bez argumentów otwiera GUI', Boolean(window.slots[10]), `tytuł=${titleText(window)}`)
  record('paginacja dla ponad 28 homeów', Boolean(window.slots[53]) && titleText(window).includes('1/2'),
    `tytuł=${titleText(window)}, slot 53=${itemName(window, 53)}`)

  try {
    await bot.clickWindow(10, 1, 0)
  } catch (_) {}
  await sleep(800)
  window = bot.currentWindow
  record('PPM przełącza ulubione', messages.some(line => line.includes('Dodano lh00 do ulubionych')),
    `slot 10 po odświeżeniu=${window ? itemName(window, 10) : 'brak GUI'}`)
  await command('/homegui description lh00 testowy opis')

  window = await openGui('/homes')
  record('/homes otwiera to samo GUI', Boolean(window.slots[10]), `tytuł=${titleText(window)}`)
  window = await openGui('/homegui')
  record('/homegui otwiera to samo GUI', Boolean(window.slots[10]), `tytuł=${titleText(window)}`)

  window = await openGui('/home')
  try {
    await bot.clickWindow(53, 0, 0)
  } catch (_) {}
  await sleep(800)
  window = bot.currentWindow
  record('przejście na drugą stronę', window && titleText(window).includes('2/2'), `tytuł=${titleText(window)}`)
}

async function afterRestart () {
  let window = await openGui('/home')
  record('/home bez argumentów otwiera GUI po restarcie', Boolean(window.slots[10]), `tytuł=${titleText(window)}`)
  record('paginacja zachowana po restarcie', Boolean(window.slots[53]) && titleText(window).includes('1/2'),
    `tytuł=${titleText(window)}, slot 53=${itemName(window, 53)}`)
  try {
    await bot.clickWindow(10, 1, 0)
  } catch (_) {}
  await sleep(800)
  record('ulubione przetrwało restart', messages.some(line => line.includes('Usunięto lh00 z ulubionych')),
    `tytuł=${titleText(bot.currentWindow)}`)
  window = await openGui('/homes')
  record('/homes działa po restarcie', Boolean(window.slots[10]), `tytuł=${titleText(window)}`)
  window = await openGui('/homegui')
  record('/homegui działa po restarcie', Boolean(window.slots[10]), `tytuł=${titleText(window)}`)
  window = await openGui('/home')
  try {
    await bot.clickWindow(53, 0, 0)
  } catch (_) {}
  await sleep(800)
  record('druga strona działa po restarcie', titleText(bot.currentWindow).includes('2/2'),
    `tytuł=${titleText(bot.currentWindow)}`)
  if (bot.currentWindow) bot.closeWindow(bot.currentWindow)
  await command('/delhome lh_alpha', 150)
  await command('/delhome lh_beta', 150)
  for (let index = 0; index < 29; index++) {
    await command(`/delhome lh${String(index).padStart(2, '0')}`, 550)
  }
  await sleep(500)
  window = await openGui('/home')
  record('stan pusty po usunięciu testowych homeów', itemName(window, 28) === 'flower_pot', `slot 28=${itemName(window, 28)}`)
}

async function cleanupAfterRestart () {
  for (let index = 13; index < 29; index++) {
    await command(`/delhome lh${String(index).padStart(2, '0')}`, 1200)
  }
  await sleep(500)
  const window = await openGui('/home')
  record('stan pusty po usunięciu testowych homeów', Boolean(window.slots[28]),
    `tytuł=${titleText(window)}, slot 28=${itemName(window, 28)}`)
}

;(async () => {
  try {
    await waitForSpawn()
    await sleep(600)
    if (stage === 'before-restart' || stage === 'resume-before-restart') await beforeRestart()
    else if (stage === 'cleanup-after-restart') await cleanupAfterRestart()
    else await afterRestart()
  } catch (error) {
    process.stderr.write(`${error.stack}\n`)
    process.exitCode = 1
  } finally {
    const output = { stage, results, messages }
    fs.mkdirSync('target', { recursive: true })
    fs.writeFileSync(`target/live-${stage}.json`, JSON.stringify(output, null, 2))
    if (results.some(result => !result.passed)) process.exitCode = 1
    if (bot.player) bot.quit('done')
    setTimeout(() => process.exit(process.exitCode || 0), 500)
  }
})()
